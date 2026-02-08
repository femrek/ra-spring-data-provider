import React, { useState } from 'react';
import { Admin, Resource, List, Datagrid, TextField, EmailField, Create, SimpleForm, TextInput, Edit, EditButton, DeleteButton, useListContext, useUpdateMany, useRefresh, useNotify, useUnselectAll, Show, SimpleShowLayout, ReferenceField, ReferenceManyField, ShowButton, NumberInput } from 'react-admin';
import { Dialog, DialogTitle, DialogContent, DialogActions, TextField as MuiTextField, Button as MuiButton } from '@mui/material';
import raSpringDataProvider from '../src/index.ts';

// Custom data provider to adapt to our Spring Boot API
const dataProvider = raSpringDataProvider('http://localhost:8081/api');

// Custom Bulk Update Button with Dialog
const BulkUpdateRoleButton = () => {
  const [open, setOpen] = useState(false);
  const [newRole, setNewRole] = useState('');
  const { selectedIds } = useListContext();
  const [updateMany, { isLoading }] = useUpdateMany();
  const refresh = useRefresh();
  const notify = useNotify();
  const unselectAll = useUnselectAll('users');

  const handleClick = () => {
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
    setNewRole('');
  };

  const handleUpdate = async () => {
    try {
      await updateMany(
        'users',
        { ids: selectedIds, data: { role: newRole } },
        {
          onSuccess: () => {
            notify('Users updated successfully', { type: 'success' });
            refresh();
            unselectAll();
            handleClose();
          },
          onError: () => {
            notify('Error: Users not updated', { type: 'error' });
          }
        }
      );
    } catch (error) {
      notify('Error: Users not updated', { type: 'error' });
    }
  };

  return (
    <>
      <MuiButton 
        onClick={handleClick}
        disabled={selectedIds.length === 0}
        variant="contained"
        size="small"
      >
        Update Role
      </MuiButton>
      <Dialog open={open} onClose={handleClose}>
        <DialogTitle>Update Role for Selected Users</DialogTitle>
        <DialogContent>
          <MuiTextField
            autoFocus
            margin="dense"
            label="New Role"
            type="text"
            fullWidth
            value={newRole}
            onChange={(e) => setNewRole(e.target.value)}
            variant="standard"
          />
        </DialogContent>
        <DialogActions>
          <MuiButton onClick={handleClose}>Cancel</MuiButton>
          <MuiButton 
            onClick={handleUpdate} 
            disabled={isLoading || !newRole}
            variant="contained"
          >
            Update
          </MuiButton>
        </DialogActions>
      </Dialog>
    </>
  );
};

// Bulk Actions
const UserBulkActionButtons = () => (
  <>
    <BulkUpdateRoleButton />
  </>
);

// Users List Component
const UserList = () => (
  <List>
    <Datagrid bulkActionButtons={<UserBulkActionButtons />}>
      <TextField source="id" />
      <TextField source="name" />
      <EmailField source="email" />
      <TextField source="role" />
      <ShowButton />
      <EditButton />
      <DeleteButton />
    </Datagrid>
  </List>
);

// Users Create Component
const UserCreate = () => (
  <Create redirect="list">
    <SimpleForm>
      <TextInput source="name" required />
      <TextInput source="email" type="email" required />
      <TextInput source="role" />
    </SimpleForm>
  </Create>
);

// Users Edit Component
const UserEdit = () => (
  <Edit redirect="list">
    <SimpleForm>
      <TextInput source="name" required />
      <TextInput source="email" type="email" required />
      <TextInput source="role" />
    </SimpleForm>
  </Edit>
);

// Users Show Component with Posts (getManyReference test)
const UserShow = () => (
  <Show>
    <SimpleShowLayout>
      <TextField source="id" />
      <TextField source="name" />
      <EmailField source="email" />
      <TextField source="role" />
      <ReferenceManyField
        reference="posts"
        target="userId"
        label="User's Posts"
      >
        <Datagrid>
          <TextField source="id" />
          <TextField source="title" />
          <TextField source="content" />
          <TextField source="status" />
        </Datagrid>
      </ReferenceManyField>
    </SimpleShowLayout>
  </Show>
);

// Posts List Component
const PostList = () => (
  <List>
    <Datagrid>
      <TextField source="id" />
      <TextField source="title" />
      <TextField source="content" />
      <ReferenceField source="userId" reference="users" label="Author">
        <TextField source="name" />
      </ReferenceField>
      <TextField source="status" />
      <EditButton />
      <DeleteButton />
    </Datagrid>
  </List>
);

// Posts Create Component
const PostCreate = () => (
  <Create redirect="list">
    <SimpleForm>
      <TextInput source="title" required />
      <TextInput source="content" multiline rows={4} required />
      <NumberInput source="userId" required />
      <TextInput source="status" />
    </SimpleForm>
  </Create>
);

// Posts Edit Component
const PostEdit = () => (
  <Edit redirect="list">
    <SimpleForm>
      <TextInput source="title" required />
      <TextInput source="content" multiline rows={4} required />
      <NumberInput source="userId" required />
      <TextInput source="status" />
    </SimpleForm>
  </Edit>
);

const App = () => (
  <Admin dataProvider={dataProvider}>
    <Resource 
      name="users" 
      list={UserList} 
      create={UserCreate} 
      edit={UserEdit}
      show={UserShow}
    />
    <Resource 
      name="posts" 
      list={PostList} 
      create={PostCreate} 
      edit={PostEdit}
    />
  </Admin>
);

export default App;
